package io.sicredi.aberturadecontasalarioefetivador.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.OracleCodec;

@UtilityClass
public class EscapeSqlInjectionUtils {

    public static List<String> escapeListString(Collection<String> list) {
        return list != null ? list.stream().map(EscapeSqlInjectionUtils::escapeString).collect(Collectors.toList()) : null;
    }

    public static String escapeString(String string) {
        return ESAPI.encoder().encodeForSQL(new OracleCodec(), string);
    }
}
