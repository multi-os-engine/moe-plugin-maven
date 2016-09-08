/*
Copyright (C) 2016 Migeran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.moe.maven;

public class OptionsBuilder {
    private final StringBuilder builder = new StringBuilder();

    OptionsBuilder push(String value) {
        builder.append(",").append(value.replaceAll(",", "\\,"));
        return this;
    }

    @Override
    public String toString() {
        return builder.length() == 1 ? "" : ("-Pmoe.launcher.options=" + builder.substring(1));
    }
}
