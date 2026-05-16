package org.lab.stall_manage.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

//生成解析token
@Component
public class JwtToken {

    /**
     * 生成token
     * @param secretKey
     * @param time
     * @param claims
     * @return String token
     */
    public static String createToken(String secretKey, long time, Map<String,Object> claims)
    {
        long exp=System.currentTimeMillis()+time;
        return Jwts.builder().
                setClaims(claims).
                setExpiration(new Date(exp)).
                signWith(SignatureAlgorithm.HS256,secretKey).
                compact();
    }

    /**
     * 解析token
     * @param secretKey
     * @param token
     * @return Claims
     */
    public static Claims parseToken(String secretKey, String token)
    {
        return Jwts.parser().
                setSigningKey(secretKey).
                parseClaimsJws(token).
                getBody();
    }
}
