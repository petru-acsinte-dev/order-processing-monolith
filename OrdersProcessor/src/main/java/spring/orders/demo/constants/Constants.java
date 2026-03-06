package spring.orders.demo.constants;

import java.util.UUID;

public final class Constants {

	// This class should not be instantiated
	private Constants() {}

	public static final String ADMIN = "ADMIN"; //$NON-NLS-1$

	public static final UUID ADMIN_UUID0 = UUID.fromString("00000000-0000-0000-0000-000000000000"); //$NON-NLS-1$

	public static final String ADMIN_ROLE = "ROLE_ADMIN"; //$NON-NLS-1$

	public static final String USERS_PATH = "/users"; //$NON-NLS-1$

	public static final String PRODUCTS_PATH = "/products"; //$NON-NLS-1$

	public static final String LOGIN_PATH = "/login/auth"; //$NON-NLS-1$

	public static final String PARAM_EXTERNAL_ID = "externalId"; //$NON-NLS-1$

	public static final String BEARER = "Bearer "; //$NON-NLS-1$

    public static final String V3_API_DOCS = "/v3/api-docs/**"; //$NON-NLS-1$
	public static final String SWAGGER_UI = "/swagger-ui/**"; //$NON-NLS-1$
	public static final String ACTUATOR_INFO = "/actuator/info"; //$NON-NLS-1$
	public static final String ACTUATOR_HEALTH = "/actuator/health"; //$NON-NLS-1$

}
