package com.example.mamunbingoapp.data.bingo

sealed class BingoRemoteException(message: String) : Exception(message) {
    class NetworkUnavailable : BingoRemoteException("network_unavailable")
    class Timeout : BingoRemoteException("timeout")
    class HttpStatus(val statusCode: Int) : BingoRemoteException("http_$statusCode")
    class InvalidStatus : BingoRemoteException("invalid_status")
    class InvalidDraw : BingoRemoteException("invalid_draw")
}

class BingoDrawNotFoundException : Exception("draw_not_found")
