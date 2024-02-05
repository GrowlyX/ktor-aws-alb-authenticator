package io.liftgate.oss.auth

import com.rbinternational.awstools.awsjwtvalidator.AWSAlbUserClaimsTokenValidator
import io.ktor.http.auth.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

/**
 * Handles authentication for JWTs from AWS' ALB authentication headers.
 *
 * @author GrowlyX
 * @since 2/4/2024
 */
const val AmznOidcAuthHeader = "X-Amzn-Oidc-Data"

class AWSAlbAuthenticationProvider internal constructor(private val config: Config) : AuthenticationProvider(config)
{
    class Config internal constructor(name: String?) : AuthenticationProvider.Config(name)
    {
        internal lateinit var realm: String
        internal lateinit var tokenValidator: AWSAlbUserClaimsTokenValidator
        internal lateinit var audience: String
        internal lateinit var issuer: String

        fun tokenValidator(tokenValidator: AWSAlbUserClaimsTokenValidator) =
            apply { this.tokenValidator = tokenValidator }

        fun audience(audience: String) =
            apply { this.audience = audience }

        fun issuer(issuer: String) =
            apply { this.issuer = issuer }

        fun realm(realm: String) =
            apply { this.realm = realm }

        fun build() = AWSAlbAuthenticationProvider(this)
    }

    override suspend fun onAuthenticate(context: AuthenticationContext)
    {
        suspend fun unauthorized() = context.call.respond(
            UnauthorizedResponse(
                HttpAuthHeader.Parameterized(
                    "Bearer",
                    mapOf(HttpAuthHeader.Parameters.Realm to config.realm)
                )
            )
        )

        val jwtToken = context.call.request.headers[AmznOidcAuthHeader]
            ?: return unauthorized()

        val validateTokensFor = kotlin
            .runCatching { config.tokenValidator.validateToken(jwtToken) }
            .getOrNull()
            ?: return unauthorized()

        if (
            validateTokensFor.header["client"] != config.audience ||
            validateTokensFor.body.issuer != config.issuer
        )
        {
            return unauthorized()
        }

        context.principal(
            name,
            AWSAlbPrincipal(claims = validateTokensFor.body)
        )
    }
}

fun AuthenticationConfig.applicationLoadBalancerJWT(
    name: String? = null,
    configure: AWSAlbAuthenticationProvider.Config.() -> Unit
)
{
    val provider = AWSAlbAuthenticationProvider.Config(name).apply(configure).build()
    register(provider)
}

