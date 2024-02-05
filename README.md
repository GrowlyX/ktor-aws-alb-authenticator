# Ktor AWS ALB Authenticator Plugin
Allows you to authenticate JWT tokens from ALB-forwarded `X-Amzn-*` headers.

## Usage:
```kotlin
applicationLoadBalancerJWT {
    issuer("https://cognito-idp.region.amazonaws.com/user-pool-id") // When using Cognito, the issue should be your user pool URL
    audience("abcdefghijklmnop") // When using Cognito, your audience should be your Cognito client ID
    realm("liftgateOSS")
    
    // uses the default eu-central-1 public key provider
    tokenValidator(
        AWSAlbUserClaimsTokenValidator()
    )
    
    // or... you can provide your own public keys endpoint
    tokenValidatorFromJwkProvider(
        AWSAlbUserClaimsJwkProvider.createProvider(
            "https://public-keys.auth.elb.us-east-1.amazonaws.com"
        )
    )
}
```
