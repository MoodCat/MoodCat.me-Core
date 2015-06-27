package me.moodcat.backend.rooms;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class RoomBackendModule extends AbstractModule {

    @Override
    protected void configure() {
        this.install(new FactoryModuleBuilder().build(SongInstanceFactory.class));
        this.install(new FactoryModuleBuilder().build(RoomInstanceFactory.class));
        this.install(new FactoryModuleBuilder().build(RoomInstanceInUnitOfWorkFactory.class));
    }

}
