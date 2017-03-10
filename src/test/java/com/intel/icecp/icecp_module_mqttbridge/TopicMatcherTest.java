/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.icecp.icecp_module_mqttbridge;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TopicMatcherTest {

    @Test
    public void ReturnTrueWhenSubscribedToCorrectTopicTest () {
        assertTrue (TopicMatcher.isTopicSubscribed("iot-2/cmd/+/fmt/json", "iot-2/cmd/lighton/fmt/json"));
        assertTrue (TopicMatcher.isTopicSubscribed("iot-2/#", "iot-2/cmd/lighton/fmt/json"));
    }

    @Test
    public void ReturnFalseWhenSubscribedToIncorrectTopicTest () {
        assertFalse (TopicMatcher.isTopicSubscribed("iot-2/cmd/+/xfmt/json", "iot-2/cmd/lighton/fmt/json"));
    }

    @Test
    public void ReturnFalseWhenInconsistentFirst$InSubAndTopic() {
        assertFalse(TopicMatcher.isTopicSubscribed("$iot-2/cmd/+/fmt/json", "iot-2/cmd/+/fmt/json"));
        assertFalse(TopicMatcher.isTopicSubscribed("iot-2/cmd/+/fmt/json", "$iot-2/cmd/+/fmt/json"));
    }
}
