package ipz.bookstore.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

// Builds and signs JWTs (Task 8). Reading/validating tokens comes in the next lab.
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Token lifetime in milliseconds
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // the login, i.e. email
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .claim("authorities", authorities)
                .signWith(getSignInKey())
                .compact();
    }

    // Same key is used to sign here and to verify later
    public SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
