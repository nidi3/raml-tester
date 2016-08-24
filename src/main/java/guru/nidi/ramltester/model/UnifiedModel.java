package guru.nidi.ramltester.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UnifiedModel {
    public static <T> List<T> typeDelegates(List<UnifiedType> parameters) {
        final List<T> res = new ArrayList<>();
        for (final UnifiedType p : parameters) {
            res.add((T)p.delegate());
        }
        return res;
    }
}
