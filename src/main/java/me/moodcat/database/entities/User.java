package me.moodcat.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User entity.
 */
@Data
@Entity
@Table(name = "user")
@EqualsAndHashCode(of = {
	"id"
})
public class User {

	/**
	 * The unique id of the user.
	 *
	 * @param id
	 *            The new Id to set.
	 * @return The id of the user.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	/**
	 * The unique id of the user.
	 *
	 * @param id
	 *            The new Id to set.
	 * @return The id of the user.
	 */
	@Column(name = "soundcloud_id", nullable = true, unique = true)
	private Integer soundcloud_id;

	/**
	 * The name of this user.
	 *
	 * @param name
	 *            The name to set.
	 * @return The name of this user.
	 */
	@Column(name = "name", nullable = false, unique = true)
	private String name;

	/**
	 * Soundcloud OAuth access token.
	 *
	 * See: https://developers.soundcloud.com/docs/api/reference#token
	 */
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "access_token", nullable = true)
	private String accessToken;

}
