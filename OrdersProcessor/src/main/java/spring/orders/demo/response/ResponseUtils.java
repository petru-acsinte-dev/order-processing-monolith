package spring.orders.demo.response;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import spring.orders.demo.constants.Constants;

/**
 * Converts service paged responses to Swagger friendly types
 */
public class ResponseUtils {

	private ResponseUtils() {}

	public static <T> ResponseEntity<PagedResponse<T>> getPagedResponse(Page<T> page) {
		final Sort sortBy = page.getSort();
		final String nextLink = (page.hasNext()) ?
				buildLink(page.getNumber() + 1, page.getSize(), sortBy) : null;
        final String prevLink = (page.hasPrevious()) ?
        		buildLink(page.getNumber() - 1, page.getSize(), sortBy) : null;

        final HttpHeaders headers = new HttpHeaders();
        if (null != nextLink) {
        	headers.add(Constants.LINK_RESPONSE_HEADER,
        		String.format(Constants.LINK_NEXT_TEMPLATE, nextLink));
        }
        if (null != prevLink) {
        	headers.add(Constants.LINK_RESPONSE_HEADER,
        		String.format(Constants.LINK_PREV_TEMPLATE, prevLink));
        }
        return new ResponseEntity<>(PagedResponse.from(page, Function.identity()), headers, HttpStatus.OK);
	}

	private static String buildLink(int pageNo, int pageSize, Sort sortBy) {
		if (null == sortBy) {
			return String.format(Constants.PAGE_LINK_TEMPLATE, Constants.USERS_PATH, pageNo, pageSize);
		}
		return String.format(Constants.PAGE_LINK_SORT_TEMPLATE, Constants.USERS_PATH, pageNo, pageSize, getSortParams(sortBy));
	}

	private static String getSortParams(Sort sortBy) {
		return sortBy.stream()
				.map(f -> String.format("&sort=%s,%s",  //$NON-NLS-1$
										f.getProperty(),
										f.getDirection().name().toLowerCase()))
				.collect(Collectors.joining());
	}

}
