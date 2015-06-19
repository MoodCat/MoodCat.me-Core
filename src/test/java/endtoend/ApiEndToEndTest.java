package endtoend;

import static org.junit.Assert.assertEquals;
import me.moodcat.api.models.RoomModel;
import me.moodcat.api.models.SongModel;

import org.junit.Test;

public class ApiEndToEndTest extends EndToEndTest {

    @Test
    public void canRetrieveRooms() {
        RoomModel room = this.performGETRequest(RoomModel.class, "rooms/1");
        
        assertEquals(1, room.getId().intValue());
        assertEquals(1, room.getNowPlaying().getSong().getId().intValue());
    }
    
    @Test
    public void canRetrieveSongs() {
        SongModel song = this.performGETRequest(SongModel.class, "songs/1");
        
        assertEquals(1, song.getId().intValue());
        assertEquals(202330997, song.getSoundCloudId().intValue());
    }

}
