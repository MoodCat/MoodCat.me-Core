/**
 * APIs are annotated with {@link javax.ws.rs.Path @Path} which are bound at
 * {@link me.moodcat.core.MoodcatServletModule#configure(com.google.inject.Binder) configuration
 * with bindAPIclasses}.
 * APIs use {@link me.moodcat.database.controllers DAOs} which define the database connection
 * interface.
 *
 * @author MoodCat
 */
package me.moodcat.api;
