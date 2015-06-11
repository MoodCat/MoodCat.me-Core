package me.moodcat.api.filters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import me.moodcat.backend.UserBackend;
import me.moodcat.core.mappers.NotAuthorizedExceptionMapper;
import me.moodcat.database.entities.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterTest {
    
    private static final String TOKEN = "token";

    @InjectMocks
    private AuthorizationFilter filter;
    
    @Mock
    private UserBackend userBackend;

    @Mock
    private NotAuthorizedExceptionMapper notAuthorizedExceptionMapper;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private MultivaluedMap<String, String> multiValuedMap;

    @Mock
    private Response response;
    
    @Before
    public void setUp() {
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getQueryParameters()).thenReturn(multiValuedMap);
        when(multiValuedMap.getFirst(AuthorizationFilter.TOKEN_PARAMETER)).thenReturn(TOKEN);
        
        when(notAuthorizedExceptionMapper.toResponse(any())).thenReturn(response);
    }
    
    @Test
    public void filtersNonExistingUser() throws IOException {
        when(userBackend.loginUsingSoundCloud(TOKEN)).thenThrow(new NotAuthorizedException("Invalid token."));
        
        this.filter.filter(containerRequestContext);
        
        verify(containerRequestContext).abortWith(response);
    }
    
    @Test
    public void doesNotFilterValidUser() throws IOException {
        User user = mock(User.class);
        when(userBackend.loginUsingSoundCloud(TOKEN)).thenReturn(user);
        
        this.filter.filter(containerRequestContext);
        
        verify(containerRequestContext).setProperty(anyString(), eq(user));
    }
    
    @Test
    public void doesNotFilterCallWhichShouldNotRequireAuthorization() throws IOException {
        when(multiValuedMap.getFirst(AuthorizationFilter.TOKEN_PARAMETER)).thenReturn("");
        
        this.filter.filter(containerRequestContext);
        
        verify(containerRequestContext, never()).abortWith(any());
        verify(containerRequestContext, never()).setProperty(anyString(), any());
    }

}
