package com.cts.logichain360.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Service
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private UserDetailsService userDetailsService;
    private JWTUtil jwtUtil;
   
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		
		//every http req has headers, This reads the one called Authorization. The client is expected to send: Authorization: Bearer token..
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        
        //get the jwt token and extract the username using extractUsername()
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            try{
                username = jwtUtil.extractUsername(jwt);
            }catch(Exception e){
                username = null;
            }
        }
        if (username != null) {


        	//Database call, fetches entire user object from DB, needed to get role from user obj as token only carries username
        	//could have used userepo to gte the username , but we are following the conventions here
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(!userDetails.isEnabled()){
                chain.doFilter(request,response);
                return;
            }


                //spring confirms who the person is and what they allowed to do.
            //three arguments are: the user object, credentials(null as we dont need it anymore) and user's roles
            //IF TOKEN HAS EXPIRED OR NOT
            if (jwtUtil.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                //like pinning badge to current req thread, any code in app can call this fn to know who is sending req
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        //passes req to next filter
        chain.doFilter(request, response);
    }
	
	//to do :handle invalidate tokens explicitly
}