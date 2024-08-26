/*
 * Copyright [2017] Wikimedia Foundation
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

package com.o19s.es.ltr.ranker.catboost;

import ai.catboost.CatBoostError;
import ai.catboost.CatBoostModel;
import com.o19s.es.ltr.feature.FeatureSet;
import com.o19s.es.ltr.ranker.DenseFeatureVector;
import com.o19s.es.ltr.ranker.DenseLtrRanker;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.RamUsageEstimator;
import org.opensearch.common.collect.Tuple;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Simple linear ranker that applies a dot product based
 * on the provided weights array.
 */
public class CatBoostRanker extends DenseLtrRanker implements Accountable {
    private final CatBoostModel model;
    private final Map<String, Tuple<Character, Integer>> features = new HashMap<>();
    private int catFeaturesCount = 0;
    private int floatFeaturesCount = 0;

    public CatBoostRanker(String modelStr, FeatureSet set) throws CatBoostError, IOException {
        this.model = CatBoostModel.loadModel(modelStr.getBytes(), "json");
        for (var feature : model.getFeatures()) {
            if (set.hasFeature(feature.getName())) {
                var c = 'f';
                if (feature instanceof CatBoostModel.CatFeature) {
                    c = 'c';
                    catFeaturesCount++;
                } else {
                    floatFeaturesCount++;
                }

                features.put(feature.getName(), Tuple.tuple(c, feature.getFlatFeatureIndex()));
            }
        }
    }

    @Override
    public String name() {
        return "catboost";
    }

    @Override
    public float score(DenseFeatureVector point) {
        float[] scores = point.scores;
        float[] floatFeatures = new float[floatFeaturesCount];
        String[] categoricalFeatures = new String[catFeaturesCount];
        int catFeatureIndex = 0;
        int floatFeatureIndex = 0;
        for (int i = 0; i < scores.length; i++) {
            var mFeature = model.getFeatureNames()[i];
            var feature = features.get(mFeature);

            if (feature.v1() == 'c') {
                categoricalFeatures[catFeatureIndex++] = String.valueOf(scores[i]);
            } else {
                floatFeatures[floatFeatureIndex++] = scores[i];
            }
        }
		try {
			var prediction = model.predict(floatFeatures, categoricalFeatures);
            return (float) prediction.get(0, 0);
		} catch (CatBoostError e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    protected int size() {
        return features.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatBoostRanker)) return false;
        CatBoostRanker that = (CatBoostRanker) o;
        return catFeaturesCount == that.catFeaturesCount && floatFeaturesCount == that.floatFeaturesCount && Objects.equals(model, that.model) && Objects.equals(features, that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, features, catFeaturesCount, floatFeaturesCount);
    }

    /**
     * Return the memory usage of this object in bytes. Negative values are illegal.
     */
    @Override
    public long ramBytesUsed() {
        return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + RamUsageEstimator.sizeOf(size());
    }
}
