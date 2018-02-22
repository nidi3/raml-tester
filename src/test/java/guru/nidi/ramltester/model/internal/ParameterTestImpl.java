/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v08.parameters.IntegerTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.NumberTypeDeclaration;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.parameters.StringTypeDeclaration;
import org.raml.v2.api.model.v08.system.types.MarkdownString;

import java.util.List;

public class ParameterTestImpl implements Parameter, IntegerTypeDeclaration, NumberTypeDeclaration, StringTypeDeclaration {
    private String type;
    private Double minimum, maximum;
    private Integer minLength, maxLength;
    private List<String> enumeration;
    private String pattern;
    private boolean required, repeat;
    private String name;

    public Type08 asType08() {
        return new Type08(this);
    }

    public static List<RamlType> asType08(List<Parameter>parameters){
        return Type08.of(parameters);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String displayName() {
        return null;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Boolean required() {
        return required;
    }

    @Override
    public String defaultValue() {
        return null;
    }

    @Override
    public String example() {
        return null;
    }

    @Override
    public Boolean repeat() {
        return repeat;
    }

    @Override
    public MarkdownString description() {
        return null;
    }

    @Override
    public Double minimum() {
        return minimum;
    }

    @Override
    public Double maximum() {
        return maximum;
    }

    @Override
    public String pattern() {
        return pattern;
    }

    @Override
    public List<String> enumValues() {
        return enumeration;
    }

    @Override
    public Integer minLength() {
        return minLength;
    }

    @Override
    public Integer maxLength() {
        return maxLength;
    }
}
