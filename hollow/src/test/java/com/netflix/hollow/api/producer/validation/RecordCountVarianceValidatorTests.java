/*
 *
 *  Copyright 2017 Netflix, Inc.
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
package com.netflix.hollow.api.producer.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.InMemoryBlobStore;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.HollowProducer.Populator;
import com.netflix.hollow.api.producer.HollowProducer.Validator.ValidationException;
import com.netflix.hollow.api.producer.HollowProducer.WriteState;
import com.netflix.hollow.api.producer.fs.HollowInMemoryBlobStager;
import com.netflix.hollow.api.producer.validation.ProducerValidationTests.TypeWithPrimaryKey;

public class RecordCountVarianceValidatorTests {
    private InMemoryBlobStore blobStore;
    
    @Before
    public void setUp() {
        blobStore = new InMemoryBlobStore();
    }
    
	@Test
	public void failTestTooManyAdded() {
		try {
			HollowProducer producer = HollowProducer.withPublisher(blobStore)
					.withBlobStager(new HollowInMemoryBlobStager())
					.withValidator(new RecordCountVarianceValidator("TypeWithPrimaryKey", 1f)).build();

			producer.runCycle(new Populator() {
				public void populate(WriteState newState) throws Exception {
					newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
					newState.add(new TypeWithPrimaryKey(1, "Angelina Jolie", "as;dlkfjasd;l"));
				}
			});

			producer.runCycle(new Populator() {
				public void populate(WriteState newState) throws Exception {
					newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
					newState.add(new TypeWithPrimaryKey(2, "Angelina Jolie", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(3, "Angelina Jolie1", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(4, "Angelina Jolie2", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(5, "Angelina Jolie3", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(6, "Angelina Jolie4", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(7, "Angelina Jolie5", "as;dlkfjasd;l"));
				}
			});
			Assert.fail();
		} catch (ValidationException expected) {
			Assert.assertEquals(1, expected.getIndividualFailures().size());
			//System.out.println("Message:"+expected.getIndividualFailures().get(0).getMessage());
			Assert.assertTrue(expected.getIndividualFailures().get(0).getMessage()
					.startsWith("RecordCountVarianceValidator for type TypeWithPrimaryKey failed."));
		}
	}
	
	@Test
	public void failTestTooManyRemoved() {
		try {
			HollowProducer producer = HollowProducer.withPublisher(blobStore)
					.withBlobStager(new HollowInMemoryBlobStager())
					.withValidator(new RecordCountVarianceValidator("TypeWithPrimaryKey", 1f)).build();

			producer.runCycle(new Populator() {
				public void populate(WriteState newState) throws Exception {
					newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
					newState.add(new TypeWithPrimaryKey(1, "Angelina Jolie", "as;dlkfjasd;l"));
				}
			});
			
			producer.runCycle(new Populator() {
				public void populate(WriteState newState) throws Exception {
					newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
					newState.add(new TypeWithPrimaryKey(1, "Angelina Jolie", "as;dlkfjasd;l"));
					newState.add(new TypeWithPrimaryKey(1, "Bruce Willis", "as;dlkfjasd;l"));
				}
			});
			Assert.fail();
		} catch (ValidationException expected) {
			//System.out.println("Message:"+expected.getIndividualFailures().get(0).getMessage());
			Assert.assertEquals(1, expected.getIndividualFailures().size());
			Assert.assertTrue(expected.getIndividualFailures().get(0).getMessage()
					.startsWith("RecordCountVarianceValidator for type TypeWithPrimaryKey failed."));
		}
	}
	
	@Test
	public void passTestNoMoreChangeThanExpected() {
		HollowProducer producer = HollowProducer.withPublisher(blobStore).withBlobStager(new HollowInMemoryBlobStager())
				.withValidator(new RecordCountVarianceValidator("TypeWithPrimaryKey", 50f)).build();

		// runCycle(producer, 1);
		producer.runCycle(new Populator() {

			public void populate(WriteState newState) throws Exception {
				newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
				newState.add(new TypeWithPrimaryKey(1, "Angelina Jolie", "as;dlkfjasd;l"));
			}
		});

		producer.runCycle(new Populator() {

			public void populate(WriteState newState) throws Exception {
				newState.add(new TypeWithPrimaryKey(1, "Brad Pitt", "klsdjfla;sdjkf"));
				newState.add(new TypeWithPrimaryKey(2, "Angelina Jolie", "as;dlkfjasd;l"));
				newState.add(new TypeWithPrimaryKey(7, "Bruce Willis", "as;dlkfjasd;l"));
			}
		});
		HollowConsumer consumer = HollowConsumer.withBlobRetriever(blobStore).build();
		consumer.triggerRefresh();
		Assert.assertEquals(3,consumer.getStateEngine().getTypeState("TypeWithPrimaryKey").getPopulatedOrdinals().cardinality());
	}
}
