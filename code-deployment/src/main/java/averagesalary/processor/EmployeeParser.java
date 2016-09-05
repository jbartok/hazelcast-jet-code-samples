/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package averagesalary.processor;

import averagesalary.model.Employee;
import com.hazelcast.jet.Processor;
import com.hazelcast.jet.io.Pair;
import com.hazelcast.jet.runtime.InputChunk;
import com.hazelcast.jet.runtime.OutputCollector;

/**
 * Reads a string record of employee and emits an {@link Employee} object
 */
public class EmployeeParser implements Processor<Pair<Integer, String>, Employee> {

    @Override
    public boolean process(InputChunk<Pair<Integer, String>> input,
                           OutputCollector<Employee> output,
                           String sourceName) throws Exception {

        for (Pair<Integer, String> pair : input) {
            String value = pair.getValue();
            Employee employee = new Employee();
            employee.fromString(value);
            output.collect(employee);
        }
        return true;
    }
}
