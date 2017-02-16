/*
 *
 *  Copyright 2016 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.api.consumer;

import com.netflix.hollow.core.read.engine.HollowReadStateEngine;

/**
 *  Beta API subject to change.
 * 
 * @author Tim Taylor {@literal<timt@netflix.com>}
 */
public class HollowConsumer {

    // TODO: timt: is this needed, or do we just use a HollowReadStateEngine in place of this? Created for now
    //   to have symmetry with HollowProducer.WriteState
    public static interface ReadState {
        long getVersion();
        HollowReadStateEngine getStateEngine();
    }
    
    public static interface AnnouncementRetriever {
        static final AnnouncementRetriever NO_ANNOUNCEMENTS = new AnnouncementRetriever(){
            @Override
            public long get() {
                return Long.MIN_VALUE;
            }
        };

        long get();
    }

}
