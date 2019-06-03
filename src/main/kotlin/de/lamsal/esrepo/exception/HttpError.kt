package de.lamsal.esrepo.exception

import java.lang.Exception

class HttpError(exception: Exception) : Exception(exception)