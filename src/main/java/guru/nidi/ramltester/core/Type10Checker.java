package guru.nidi.ramltester.core;

import guru.nidi.ramltester.util.Message;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.Collection;

class Type10Checker {
    private final RamlViolations violations;

    public Type10Checker(RamlViolations violations) {
        this.violations = violations;
    }

    public void check(TypeDeclaration type, Object value, Message message) {
        if (value instanceof Collection) {
            //repeat value without array type -> just caller's error message
            final String et = elementType(type);
            if (et != null) {
                doCheck(type, join((Collection<?>) value, "string".equals(et) ? "\"" : ""), message);
            }
        } else if (value == null) {
            if ("string".equals(type.type())) {
                doCheck(type, "", message);
            } else {
                violations.add(message.withInnerParam(new Message("value", "empty").withMessageParam("value.empty")));
            }
        } else {
            doCheck(type, value.toString(), message);
        }
    }

    private void doCheck(TypeDeclaration type, String value, Message message) {
        for (final ValidationResult res : type.validate(value)) {
            violations.add(message.withInnerParam(new Message("value10", value, res.getMessage())));
        }
    }

    private String elementType(TypeDeclaration type) {
        final String t = type.type();
        if (t.endsWith("[]")) {
            return t.substring(0, t.length() - 2);
        }
        if (type instanceof ArrayTypeDeclaration) {
            final String items = ((ArrayTypeDeclaration) type).items().type();
            return "array".equals(items) ? "string" : items;
        }
        return null;
    }

    private String join(Collection<?> coll, String quote) {
        final StringBuilder sb = new StringBuilder("[");
        for (final Object c : coll) {
            sb.append(quote).append(c.toString()).append(quote).append(',');
        }
        return sb.replace(sb.length() - 1, sb.length(), "]").toString();
    }

}
