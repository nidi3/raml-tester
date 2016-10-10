/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.String.format;

/**
 * Created by arielsegura on 10/9/16.
 */
public interface RamlViolationCause {

    ObjectMapper mapper = new ObjectMapper();

    String getMessage();

    default String asJson() throws JsonProcessingException {
        return mapper.writeValueAsString(new DefaultJsonMessage(getMessage()));
    }

     class DefaultJsonMessage{
        String message;

         public DefaultJsonMessage(String message) {
             this.message = message;
         }

         public String getMessage() {
             return message;
         }

         public void setMessage(String message) {
             this.message = message;
         }
     }
}
