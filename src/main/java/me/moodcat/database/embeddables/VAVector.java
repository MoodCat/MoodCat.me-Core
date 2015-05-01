package me.moodcat.database.embeddables;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Embeddable
@EqualsAndHashCode
public class VAVector {

    @Column(name = "valence")
    private double valence;

    @Column(name = "arousal")
    private double arousal;

}
