package wolox.training.services;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import wolox.training.TestUtils;
import wolox.training.config.SpringConfig;
import wolox.training.exceptions.ParseBookException;
import wolox.training.exceptions.RequestException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class OpenLibraryServiceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OpenLibraryService openLibraryService;

    private MockRestServiceServer mockRestServiceServer;

    private String openLibraryUrl = "https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data";
    private String isbn = "0385472579";
    private String bookResponse =
        "{\"ISBN:0385472579\": {\"publishers\": [{\"name\": \"Anchor Books\"}], "
            + "\"pagination\": \"159 p. :\", \"identifiers\": {\"lccn\": [\"93005405\"], \"openlibrary\": "
            + "[\"OL1397864M\"], \"isbn_10\": [\"0385472579\"], \"goodreads\": [\"979250\"], \"librarything\": "
            + "[\"192819\"]}, \"subtitle\": \"shouts of nothingness\", \"title\": \"Zen speaks\", \"url\": "
            + "\"https://openlibrary.org/books/OL1397864M/Zen_speaks\", \"number_of_pages\": 159, \"cover\": "
            + "{\"small\": \"https://covers.openlibrary.org/b/id/240726-S.jpg\", \"large\": "
            + "\"https://covers.openlibrary.org/b/id/240726-L.jpg\", \"medium\": "
            + "\"https://covers.openlibrary.org/b/id/240726-M.jpg\"}, \"subjects\": [{\"url\": "
            + "\"https://openlibrary.org/subjects/caricatures_and_cartoons\", \"name\": \"Caricatures and cartoons\"}, "
            + "{\"url\": \"https://openlibrary.org/subjects/zen_buddhism\", \"name\": \"Zen Buddhism\"}], \"publish_date\": "
            + "\"1994\", \"key\": \"/books/OL1397864M\", \"authors\": [{\"url\": "
            + "\"https://openlibrary.org/authors/OL223368A/Zhizhong_Cai\", \"name\": \"Zhizhong Cai\"}], "
            + "\"classifications\": {\"dewey_decimal_class\": [\"294.3/927\"], \"lc_classifications\": "
            + "[\"BQ9265.6 .T7313 1994\"]}, \"publish_places\": [{\"name\": \"New York\"}]}}";

    @Before
    public void setup() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void givenAValidIsbn_whenBookInfoIsCalled_thenParseBookDto()
        throws IOException, ParseBookException, RequestException {
        mockRestServiceServer
            .expect(requestTo(openLibraryUrl.replace("{isbn}", isbn)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.toStringJson(bookResponse))
            );

//        BookDto bookDto = openLibraryService.bookInfo(isbn);
//        mockRestServiceServer.verify();
//        assertThat(bookDto).isNotNull();
//        assertThat(bookDto.getISBN()).isEqualTo(isbn);
    }

}