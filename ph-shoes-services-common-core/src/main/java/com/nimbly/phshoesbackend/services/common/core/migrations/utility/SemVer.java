package com.nimbly.phshoesbackend.services.common.core.migrations.utility;

public final class SemVer {
    private SemVer() {}
    public static int compare(String a, String b) {
        String[] A = a.split("\\."); String[] B = b.split("\\.");
        for (int i=0;i<Math.max(A.length,B.length);i++) {
            int ai = i<A.length ? parse(A[i]) : 0;
            int bi = i<B.length ? parse(B[i]) : 0;
            if (ai != bi) return Integer.compare(ai, bi);
        }
        return 0;
    }
    private static int parse(String s) { try { return Integer.parseInt(s); } catch(Exception e){ return 0; } }
}
