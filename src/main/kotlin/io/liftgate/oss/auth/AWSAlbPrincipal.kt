package io.liftgate.oss.auth

import io.jsonwebtoken.Claims
import io.ktor.server.auth.*

/**
 * @author GrowlyX
 * @since 2/4/2024
 */
class AWSAlbPrincipal(claims: Claims) : Claims by claims, Principal
