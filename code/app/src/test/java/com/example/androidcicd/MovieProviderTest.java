package com.example.androidcicd;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MovieProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockMovieCollection;

    @Mock
    private DocumentReference mockDocRef;

    private MovieProvider movieProvider;

    @Before //before each test, create a new instance
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        //anything with mock annotation, use mock for it instead of default inplementations, in THIS file

        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("movies")).thenReturn(mockMovieCollection); //when this method calls, return mocked version
        when(mockMovieCollection.document()).thenReturn(mockDocRef); //when create new doc,instead of firebase doc, returns a mock locally
        when(mockMovieCollection.document(anyString())).thenReturn(mockDocRef); //when query certain doc witha key, ret mock doc reference
        // Setup the movie provider
        //mske sure we have a fresh instance for each test (so each test doesnt have dependencies on previous test)
        MovieProvider.setInstanceForTesting(mockFirestore);
        movieProvider = MovieProvider.getInstance(mockFirestore);
        }


    @Test
    public void testAddMovieSetsId() {
        // Movie to add
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);

        // Define the ID we want to set for the movie
        when(mockDocRef.getId()).thenReturn("123");

        //checks the process of adding movie to firestore
        // Add movie and check that we update our movie with the generated id
        movieProvider.addMovie(movie);
        assertEquals("Movie was not updated with correct id.", "123", movie.getId());

    /*
        Verify that we called the set method. Normally, this would call the database, but due to what we did in our setup method, this will not actually interact with the database, but the mock does track that the method was called.
    */
        verify(mockDocRef).set(movie); //verify that actually updated firestore with actual movie
        //checks: "did we call set with movie as the provider?"
    }


    @Test
    public void testDeleteMovie() {
        // Create movie and set our id
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");

        // Call the delete movie and verify the firebase delete method was called.
        movieProvider.deleteMovie(movie);
        verify(mockDocRef).delete(); //verify that delete was actually called (that firestore actually tried ot delete)
    }


    @Test(expected = IllegalArgumentException.class) //expect test to throw illegialargexc error
    public void testUpdateMovieShouldThrowErrorForDifferentIds() { //then dont wannna update id in firesotre with local id
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        // Set our ID to 1
        movie.setId("1");

        // Make sure the doc ref has a different ID
        when(mockDocRef.getId()).thenReturn("123"); //dicref has diff id than the one we set up locally

        // Call update movie, which should throw an error
        movieProvider.updateMovie(movie, "Another Title", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForEmptyName() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error due to having an empty name
        movieProvider.updateMovie(movie, "", "Another Genre", 2026); //should fail cuz empty title
    }

}
