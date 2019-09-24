package wolox.training.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import wolox.training.exceptions.ParseBookException;
import wolox.training.exceptions.RequestException;
import wolox.training.models.dtos.BookDto;

@Service
public class OpenLibraryService {

    @Value("${open.library.url}")
    private String baseUrl;

    public BookDto bookInfo(String isbn) throws IOException, ParseBookException, RequestException {
        ClientHttpResponse response = new RestTemplate()
            .getRequestFactory()
            .createRequest(URI.create(baseUrl.replace("{isbn}", isbn)), HttpMethod.GET)
            .execute();

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RequestException();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());

        JsonNode jsonDocument = json.get("ISBN:{isbn}".replace("{isbn}", isbn));

        return parseBookResponse(jsonDocument, isbn);
    }

    private BookDto parseBookResponse(JsonNode jsonDocument, String isbn)
        throws ParseBookException {
        try {
            String title = jsonDocument
                .get("title")
                .asText(null);

            String subtitle = jsonDocument
                .get("subtitle")
                .asText(null);

            String publishDate = jsonDocument
                .get("publish_date")
                .asText(null);

            int pages = jsonDocument
                .get("number_of_pages")
                .asInt(0);

            String coverImage = jsonDocument
                .get("cover")
                .get("large")
                .asText("");

            List<String> publishers = jsonDocument
                .get("publishers")
                .findValues("name")
                .stream()
                .filter(jsonNode1 -> !jsonNode1.isNull())
                .map(jsonNode -> jsonNode.asText(""))
                .collect(Collectors.toList());

            List<String> authors = jsonDocument
                .get("authors")
                .findValues("name")
                .stream()
                .filter(jsonNode1 -> !jsonNode1.isNull())
                .map(jsonNode -> jsonNode.asText(""))
                .collect(Collectors.toList());

            return new BookDto(isbn, title, subtitle, publishers, publishDate, pages, authors,
                coverImage);
        } catch (Exception ex) {
            throw new ParseBookException();
        }
    }
}
