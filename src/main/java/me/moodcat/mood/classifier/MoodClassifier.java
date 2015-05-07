package me.moodcat.mood.classifier;

import me.moodcat.database.embeddables.AcousticBrainzData;
import me.moodcat.database.embeddables.VAVector;
import me.moodcat.database.entities.Song;
import predictors.Predictor;

public class MoodClassifier implements Predictor<AcousticBrainzData, VAVector> {

    @Override
    public VAVector predict(final AcousticBrainzData data) {

        final double valence = data.getLowlevel().getDissonance().getMean()
                + data.getTonal().getKeyStrength();

        final double arousal = data.getRhythm().getBpm()
                + data.getLowlevel().getAverageLoudness()
                + data.getTonal().getTuningFrequency();

        return new VAVector(valence / 2.0, arousal / 3.0);
    }

    @Override
    public void train(final AcousticBrainzData song, final VAVector vector) {
        // This classifier does not train.
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
