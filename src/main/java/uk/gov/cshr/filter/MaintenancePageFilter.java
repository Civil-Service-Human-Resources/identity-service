package uk.gov.cshr.filter;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.cshr.utils.MaintenancePageUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Component
@Order(102)
public class MaintenancePageFilter extends OncePerRequestFilter {

	private final MaintenancePageUtil maintenancePageUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(!maintenancePageUtil.skipMaintenancePageForUser(request)) {
			response.sendRedirect("/maintenance");
			return;
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return maintenancePageUtil.shouldNotApplyMaintenancePageFilterForURI(request);
	}
}
