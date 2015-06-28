package endtoend;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.core.GenericType;

import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;
import me.moodcat.database.controllers.SongDAO;
import me.moodcat.database.entities.Song;

import org.junit.Test;

import com.google.inject.Inject;

public class ApiEndToEndTest extends EndToEndTest {

    @Inject
    private SongDAO songDAO;

    @Test
    public void canRetrieveRoom() {
        RoomModel room = this.perform(invocation -> invocation.path("rooms").path("1")
                .request()
                .get(RoomModel.class));

        assertEquals(1, room.getId().intValue());
        assertEquals(1, room.getNowPlaying().getSong().getId().intValue());
    }

    @Test
    public void canRetrieveSongs() {
        Song expected = songDAO.findById(1);

        SongModel songModel = this.perform(invocation -> invocation.path("songs").path("1")
                .request()
                .get(SongModel.class));

        assertEquals(1, songModel.getId().intValue());
        assertEquals(expected.getSoundCloudId(), songModel.getSoundCloudId());
    }

    @Test
    public void canRetrieveRooms() {
        List<RoomModel> rooms = this.perform(invocation -> invocation.path("rooms")
                .request()
                .get(new GenericType<List<RoomModel>>() {
                }));

        assertEquals(1, rooms.get(0).getId().intValue());
    }

}
