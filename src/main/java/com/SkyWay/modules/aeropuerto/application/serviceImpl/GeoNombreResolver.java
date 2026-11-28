package com.SkyWay.modules.aeropuerto.application.serviceImpl;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.function.Function;

/**
 * Resolutor geográfico 100% dinámico basado en los estándares ISO 3166-1 y CLDR del JDK.
 * No contiene nombres de países hardcodeados: construye en tiempo de ejecución el índice
 * multilingüe (ISO-2, ISO-3, nombres localizados y siglas unívocas derivadas) para todos
 * los países y territorios soportados por la JVM.
 */
public final class GeoNombreResolver {

    private static final Set<String> CONECTORES = Set.of(
            "de", "del", "la", "las", "los", "el", "y", "e", "da", "do", "das", "dos",
            "du", "des", "di", "d", "of", "the", "and", "in", "et", "und"
    );

    private static final Set<String> PALABRAS_GENERICAS_PAIS = Set.of(
            "de", "del", "la", "las", "los", "el", "y", "e", "da", "do", "das", "dos",
            "du", "des", "di", "d", "of", "the", "and", "in", "et", "und",
            "republica", "republic", "republique", "republik", "repubblica",
            "federacion", "federation", "federativa", "federal",
            "democratica", "democratic", "popular", "peoples", "islamica", "islamic",
            "arabe", "arab", "bolivariana", "plurinacional", "oriental",
            "principado", "principality", "gran", "grand", "ducado", "duchy",
            "mancomunidad", "commonwealth", "estado", "state",
            "rae", "sar", "ras", "sonderverwaltungsregion"
    );

    private static final Set<String> PALABRAS_GENERICAS_CIUDAD = Set.of(
            "de", "del", "la", "las", "los", "el", "y", "e", "da", "do", "das", "dos",
            "of", "the", "and", "ciudad", "city", "municipio", "municipality",
            "greater", "gran", "metropolitan", "metropolitana", "distrito", "district", "area"
    );

    private static final Map<String, Set<String>> ISO2_TO_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> EXACT_NAME_TO_ISO2 = new HashMap<>();

    static {
        Set<String> allIso2 = new LinkedHashSet<>(Arrays.asList(Locale.getISOCountries()));
        Map<String, List<Locale>> nativeLocalesByIso2 = new HashMap<>();

        for (Locale l : Locale.getAvailableLocales()) {
            String country = l.getCountry();
            if (country != null && country.length() == 2 && country.matches("[A-Za-z]{2}")) {
                String upper = country.toUpperCase(Locale.ROOT);
                allIso2.add(upper);
                nativeLocalesByIso2.computeIfAbsent(upper, k -> new ArrayList<>()).add(l);
            }
        }

        List<Locale> displayLocales = List.of(
                Locale.forLanguageTag("es"),
                Locale.forLanguageTag("es-ES"),
                Locale.forLanguageTag("es-419"),
                Locale.forLanguageTag("es-MX"),
                Locale.forLanguageTag("es-CL"),
                Locale.ENGLISH,
                Locale.US,
                Locale.UK,
                Locale.FRENCH,
                Locale.GERMAN,
                Locale.ITALIAN,
                Locale.forLanguageTag("pt"),
                Locale.forLanguageTag("pt-BR")
        );

        Set<String> allIso3 = new HashSet<>();
        for (String iso2 : allIso2) {
            Locale loc = crearLocaleRegion(iso2);
            if (loc == null) continue;
            try {
                String iso3 = loc.getISO3Country();
                if (iso3 != null && iso3.length() == 3) {
                    allIso3.add(iso3.toLowerCase(Locale.ROOT));
                }
            } catch (MissingResourceException ignored) {
            }
        }

        Map<String, Set<String>> candidateAcronyms = new HashMap<>();

        for (String iso2 : allIso2) {
            Locale loc = crearLocaleRegion(iso2);
            if (loc == null) continue;

            Set<String> names = new LinkedHashSet<>();
            names.add(iso2.toLowerCase(Locale.ROOT));
            try {
                String iso3 = loc.getISO3Country();
                if (iso3 != null && !iso3.isBlank()) {
                    names.add(normalizarTexto(iso3));
                }
            } catch (MissingResourceException ignored) {
            }

            List<Locale> localesToCheck = new ArrayList<>(displayLocales);
            localesToCheck.addAll(nativeLocalesByIso2.getOrDefault(iso2, List.of()));

            for (Locale dl : localesToCheck) {
                String display = loc.getDisplayCountry(dl);
                if (display == null || display.isBlank() || display.equalsIgnoreCase(iso2)) continue;

                String normFull = normalizarTexto(display);
                if (!normFull.isEmpty()) {
                    names.add(normFull);
                }

                String sinParen = normalizarTexto(display.replaceAll("\\([^)]*\\)", " "));
                if (!sinParen.isEmpty()) {
                    names.add(sinParen);
                    String sinAdmin = quitarPrefijosAdministrativos(sinParen);
                    if (!sinAdmin.isEmpty() && !sinAdmin.equals(sinParen)) {
                        names.add(sinAdmin);
                    }
                }

                if (dl.equals(Locale.ENGLISH) || "es".equals(dl.getLanguage())) {
                    String sigla = construirSigla(sinParen);
                    if (sigla.length() >= 2 && sigla.length() <= 4
                            && !allIso2.contains(sigla.toUpperCase(Locale.ROOT))
                            && !allIso3.contains(sigla)) {
                        candidateAcronyms.computeIfAbsent(sigla, k -> new HashSet<>()).add(iso2);
                    }
                }
            }

            names.remove("");
            ISO2_TO_NAMES.put(iso2, names);
            for (String n : names) {
                EXACT_NAME_TO_ISO2.putIfAbsent(n, iso2);
            }
        }

        // Registrar siglas derivadas solo cuando identifican de forma unívoca a un único país
        for (Map.Entry<String, Set<String>> entry : candidateAcronyms.entrySet()) {
            if (entry.getValue().size() == 1) {
                String iso2 = entry.getValue().iterator().next();
                ISO2_TO_NAMES.get(iso2).add(entry.getKey());
                EXACT_NAME_TO_ISO2.putIfAbsent(entry.getKey(), iso2);
            }
        }
    }

    private GeoNombreResolver() {
    }

    private static Locale crearLocaleRegion(String iso2) {
        try {
            return new Locale.Builder().setRegion(iso2).build();
        } catch (Exception e) {
            return null;
        }
    }

    private static String quitarPrefijosAdministrativos(String norm) {
        String[] tokens = norm.split(" ");
        List<String> out = new ArrayList<>();
        for (String t : tokens) {
            if (t.equals("rae") || t.equals("sar") || t.equals("ras") || t.equals("sonderverwaltungsregion")
                    || (out.isEmpty() && (t.equals("de") || t.equals("di") || t.equals("da")
                    || t.equals("r") || t.equals("a") || t.equals("s") || t.equals("chinoise")))) {
                continue;
            }
            out.add(t);
        }
        return String.join(" ", out);
    }

    private static String construirSigla(String norm) {
        String[] parts = norm.split(" ");
        if (parts.length < 2) return "";
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.length() < 3 || CONECTORES.contains(p)) continue;
            sb.append(p.charAt(0));
        }
        return sb.toString();
    }

    public static String normalizarTexto(String raw) {
        if (raw == null) return "";
        return Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Resuelve cualquier nombre de país (en cualquier idioma/variante) o código ISO-2
     * a su código canónico ISO 3166-1 alpha-2 (ej. "US", "BR", "KR", "RU", "CZ", "CL").
     */
    public static String resolverIso2(String nombrePais, String codigoIsoOpcional) {
        if (codigoIsoOpcional != null) {
            String iso = codigoIsoOpcional.trim().toUpperCase(Locale.ROOT);
            if (iso.length() == 2 && ISO2_TO_NAMES.containsKey(iso)) {
                return iso;
            }
        }
        String norm = normalizarTexto(nombrePais);
        if (norm.isEmpty()) return null;

        String exact = EXACT_NAME_TO_ISO2.get(norm);
        if (exact != null) return exact;

        List<String> queryTokens = tokensSignificativos(norm, PALABRAS_GENERICAS_PAIS);
        Set<String> querySet = new LinkedHashSet<>(queryTokens);
        String qCore = String.join(" ", queryTokens);

        String bestIso = null;
        double bestScore = 0.0;

        for (Map.Entry<String, Set<String>> entry : ISO2_TO_NAMES.entrySet()) {
            String iso2 = entry.getKey();
            for (String cand : entry.getValue()) {
                if (cand.length() <= 3) continue;
                double score = puntuarCoincidencia(norm, queryTokens, querySet, qCore, cand, PALABRAS_GENERICAS_PAIS);
                if (score > bestScore) {
                    bestScore = score;
                    bestIso = iso2;
                }
            }
        }
        return bestIso;
    }

    /**
     * Compara dinámicamente un país detectado (nombre + código ISO opcional) contra el país almacenado en BD.
     */
    public static boolean paisesCoinciden(String paisDetectado, String countryCodeIso, String paisBd) {
        String normBd = normalizarTexto(paisBd);
        if (normBd.isEmpty()) return true;

        String normDet = normalizarTexto(paisDetectado);
        if (normDet.isEmpty() && (countryCodeIso == null || countryCodeIso.isBlank())) return true;

        if (!normDet.isEmpty() && normDet.equals(normBd)) {
            return true;
        }

        String isoDet = resolverIso2(paisDetectado, countryCodeIso);
        String isoBd = resolverIso2(paisBd, null);

        if (isoDet != null && isoBd != null) {
            return isoDet.equals(isoBd);
        }

        // Respaldo para territorios o nombres personalizados fuera de ISO 3166-1
        if (isoDet != null) {
            List<String> bdTokens = tokensSignificativos(normBd, PALABRAS_GENERICAS_PAIS);
            Set<String> bdSet = new LinkedHashSet<>(bdTokens);
            String bdCore = String.join(" ", bdTokens);
            for (String cand : ISO2_TO_NAMES.getOrDefault(isoDet, Set.of())) {
                if (cand.length() <= 3) continue;
                if (puntuarCoincidencia(normBd, bdTokens, bdSet, bdCore, cand, PALABRAS_GENERICAS_PAIS) >= 0.85) {
                    return true;
                }
            }
        }

        if (!normDet.isEmpty()) {
            List<String> detTokens = tokensSignificativos(normDet, PALABRAS_GENERICAS_PAIS);
            Set<String> detSet = new LinkedHashSet<>(detTokens);
            String detCore = String.join(" ", detTokens);
            return puntuarCoincidencia(normDet, detTokens, detSet, detCore, normBd, PALABRAS_GENERICAS_PAIS) >= 0.85;
        }

        return false;
    }

    /**
     * Encuentra el mejor registro de país en una colección de BD usando resolución ISO/CLDR dinámica.
     */
    public static <T> T buscarMejorPais(Collection<T> paises, Function<T, String> extractorNombre,
                                        String paisDetectado, String countryCodeIso) {
        if (paises == null || paises.isEmpty()) return null;

        String isoDet = resolverIso2(paisDetectado, countryCodeIso);
        String normDet = normalizarTexto(paisDetectado);

        T bestIsoMatch = null;
        double bestIsoTieScore = -1.0;

        if (isoDet != null) {
            for (T item : paises) {
                if (item == null) continue;
                String nombre = extractorNombre.apply(item);
                if (nombre == null || nombre.isBlank()) continue;
                String isoItem = resolverIso2(nombre, null);
                if (isoDet.equals(isoItem)) {
                    double tie = normDet.isEmpty() ? 1.0 : similitudLevenshtein(normDet, normalizarTexto(nombre));
                    if (tie > bestIsoTieScore) {
                        bestIsoTieScore = tie;
                        bestIsoMatch = item;
                    }
                }
            }
            if (bestIsoMatch != null) {
                return bestIsoMatch;
            }
        }

        // Respaldo dinámico por similitud si no hay código ISO o el registro en BD es un territorio personalizado
        T bestFallback = null;
        double bestScore = 0.0;
        List<String> detTokens = tokensSignificativos(normDet, PALABRAS_GENERICAS_PAIS);
        Set<String> detSet = new LinkedHashSet<>(detTokens);
        String detCore = String.join(" ", detTokens);

        for (T item : paises) {
            if (item == null) continue;
            String normItem = normalizarTexto(extractorNombre.apply(item));
            if (normItem.isEmpty()) continue;

            double score = 0.0;
            if (!normDet.isEmpty()) {
                score = Math.max(score, puntuarCoincidencia(normDet, detTokens, detSet, detCore, normItem, PALABRAS_GENERICAS_PAIS));
            }
            if (isoDet != null) {
                List<String> itemTokens = tokensSignificativos(normItem, PALABRAS_GENERICAS_PAIS);
                Set<String> itemSet = new LinkedHashSet<>(itemTokens);
                String itemCore = String.join(" ", itemTokens);
                for (String cand : ISO2_TO_NAMES.getOrDefault(isoDet, Set.of())) {
                    if (cand.length() <= 3) continue;
                    score = Math.max(score, puntuarCoincidencia(normItem, itemTokens, itemSet, itemCore, cand, PALABRAS_GENERICAS_PAIS));
                }
            }
            if (score > bestScore && score >= 0.85) {
                bestScore = score;
                bestFallback = item;
            }
        }
        return bestFallback;
    }

    /**
     * Encuentra la mejor ciudad dentro de la lista del país seleccionado sin falsos positivos por subcadenas.
     */
    public static <T> T buscarMejorCiudad(Collection<T> ciudades, Function<T, String> extractorNombre, String ciudadDetectada) {
        if (ciudades == null || ciudades.isEmpty() || ciudadDetectada == null || ciudadDetectada.isBlank()) {
            return null;
        }
        String normDet = normalizarTexto(ciudadDetectada);
        if (normDet.isEmpty()) return null;

        List<String> detTokens = tokensSignificativos(normDet, PALABRAS_GENERICAS_CIUDAD);
        Set<String> detSet = new LinkedHashSet<>(detTokens);
        String detCore = String.join(" ", detTokens);

        T best = null;
        double bestScore = 0.0;

        for (T item : ciudades) {
            if (item == null) continue;
            String normItem = normalizarTexto(extractorNombre.apply(item));
            if (normItem.isEmpty()) continue;

            double score = puntuarCoincidencia(normDet, detTokens, detSet, detCore, normItem, PALABRAS_GENERICAS_CIUDAD);
            if (score > bestScore && score >= 0.84) {
                bestScore = score;
                best = item;
            }
        }
        return best;
    }

    private static double puntuarCoincidencia(String normA, List<String> tokensA, Set<String> setA, String coreA,
                                              String normB, Set<String> genericas) {
        if (normA.equals(normB)) return 1.0;

        List<String> tokensB = tokensSignificativos(normB, genericas);
        Set<String> setB = new LinkedHashSet<>(tokensB);
        String coreB = String.join(" ", tokensB);

        if (!setA.isEmpty() && setA.equals(setB)) {
            return 0.98;
        }

        // Contención estricta por palabras completas (evita errores de subcadena como "oman" en "romania" o "lima" en "colima")
        if (!setA.isEmpty() && !setB.isEmpty() && (setA.containsAll(setB) || setB.containsAll(setA))) {
            int inter = Math.min(setA.size(), setB.size());
            int union = Math.max(setA.size(), setB.size());
            double jaccard = (double) inter / union;
            if (jaccard >= 0.65) {
                return 0.85 + 0.10 * jaccard;
            }
        }

        // Similitud léxica/fonética controlada sobre el núcleo significativo
        if (coreA.length() >= 5 && coreB.length() >= 5 && tokensA.size() == tokensB.size()) {
            double simRaw = similitudLevenshtein(coreA, coreB);
            double simFon = similitudLevenshtein(claveFonetica(coreA), claveFonetica(coreB));
            double sim = Math.max(simRaw, simFon);
            if (sim >= 0.82) {
                return 0.70 + 0.20 * sim;
            }
        }

        return 0.0;
    }

    private static List<String> tokensSignificativos(String norm, Set<String> genericas) {
        List<String> res = new ArrayList<>();
        for (String t : norm.split(" ")) {
            if (!t.isEmpty() && !genericas.contains(t)) {
                res.add(t);
            }
        }
        if (res.isEmpty() && !norm.isEmpty()) {
            return Arrays.asList(norm.split(" "));
        }
        return res;
    }

    private static String claveFonetica(String s) {
        return s.replace("qu", "k")
                .replace("cz", "ch")
                .replace("tz", "ts")
                .replace("ph", "f")
                .replace("mb", "nb")
                .replace("mp", "np")
                .replaceAll("c(?=[aou])", "k")
                .replace("y", "i")
                .replace("z", "s")
                .replace("v", "b")
                .replaceAll("(?<!c)h", "")
                .replaceAll("(.)\\1+", "$1");
    }

    private static double similitudLevenshtein(String a, String b) {
        if (a.equals(b)) return 1.0;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return 1.0 - ((double) prev[b.length()] / maxLen);
    }
}
