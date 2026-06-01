import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const BASE_URL = "https://www.bingo-umweltlotterie.de/api/gewinnzahlen";

Deno.serve(async () => {
  const supabaseUrl =
    Deno.env.get("SUPABASE_URL") ?? Deno.env.get("PROJECT_URL") ?? "";
  const serviceRoleKey =
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ??
    Deno.env.get("SERVICE_ROLE_KEY") ??
    "";
  if (!supabaseUrl || !serviceRoleKey) {
    return Response.json(
      {
        ok: false,
        error:
          "Missing Supabase credentials (SUPABASE_URL + SUPABASE_SERVICE_ROLE_KEY)",
      },
      { status: 500 },
    );
  }
  const supabase = createClient(supabaseUrl, serviceRoleKey);

  try {
    const drawDate = getLatestSundayBerlin();
    const apiUrl = `${BASE_URL}/${drawDate}`;

    const res = await fetch(apiUrl, {
      headers: { "User-Agent": "MamunBingoApp/1.0" },
    });

    if (!res.ok) throw new Error(`API fetch failed: ${res.status}`);

    const data = await res.json();
    const bingo = data?.bingo;

    if (!bingo) throw new Error(`No bingo data found for ${drawDate}`);

    const drawNumbers = (bingo.drawNumbersCollection ?? [])
      .slice()
      .sort((a: any, b: any) => a.index - b.index)
      .map((item: any) => item.drawNumber);

    const superchance =
      bingo.candidatesCollection?.map((item: any) => ({
        index: item.index,
        serialNumber: item.serialNumber,
        ticket: item.ticket,
      })) ?? [];

    const { data: draw, error: drawError } = await supabase
      .from("bingo_draws")
      .upsert(
        {
          draw_date: drawDate,
          jackpot: Math.round(bingo.jackpotCurrently ?? 0),
          next_draw_at: null,
          winning_numbers: drawNumbers,
          game_amount: Math.round(bingo.gameAmount ?? 0),
          final_game_amount: Math.round(bingo.gameAmountFinalGame ?? 0),
          superchance,
          raw_data: data,
          updated_at: new Date().toISOString(),
        },
        { onConflict: "draw_date" },
      )
      .select("id")
      .single();

      if (drawError) throw new Error(JSON.stringify(drawError));

    await supabase.from("bingo_prizes").delete().eq("draw_id", draw.id);

    const prizeRows = (bingo.oddsCollection ?? []).map((item: any) => ({
      draw_id: draw.id,
      winning_class: item.winningClass,
      category: `Klasse ${item.winningClass}`,
      winner_count: item.numberOfWinners,
      amount: item.odds,
    }));

    if (prizeRows.length > 0) {
      const { error: prizeError } = await supabase
        .from("bingo_prizes")
        .insert(prizeRows);

        if (prizeError) throw new Error(JSON.stringify(prizeError));
    }

    await supabase.from("scraper_logs").insert({
      status: "success stats",
      error_message: `Saved draw ${drawDate}`,
    });

    return Response.json({
      ok: true,
      drawDate,
      numbers: drawNumbers,
      jackpot: bingo.jackpotCurrently,
      prizes: prizeRows.length,
    });
  } catch (error) {
    await supabase.from("scraper_logs").insert({
      status: "failed",
      error_message: String(error),
    });

    return Response.json({ ok: false, error: String(error) }, { status: 500 });
  }
});

function getLatestSundayBerlin(): string {
  const now = new Date();
  const berlin = new Date(
    now.toLocaleString("en-US", { timeZone: "Europe/Berlin" }),
  );

  const day = berlin.getDay();
  const diff = day === 0 ? 0 : day;

  berlin.setDate(berlin.getDate() - diff);

  const year = berlin.getFullYear();
  const month = String(berlin.getMonth() + 1).padStart(2, "0");
  const date = String(berlin.getDate()).padStart(2, "0");

  return `${year}-${month}-${date}`;
}