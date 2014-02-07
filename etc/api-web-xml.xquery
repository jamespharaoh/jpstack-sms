declare variable $module := //txt2-module;

declare variable $mode external;

<web-app xmlns="http://java.sun.com/xml/ns/j2ee" version="2.4">

	<display-name>Txt2 API</display-name>

	<!-- =============================== context params -->

	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>{ string-join ((
			for
				$module-name in (
					for $depend in $module/* [name () = 'depend-module']
					return $depend/@name,
					$module/@name
				),
				$layer-name in (
					'model',
					'web',
					'api',
					'hibernate',
					'misc'
				)
			return concat (
				'classpath:txt2/',
				replace ($module-name, '-', ''),
				'/',
				$layer-name,
				'/',
				$module-name,
				'-',
				$layer-name,
				'-beans.xml'
			)
		), ' ') }</param-value>
	</context-param>

	<context-param>
		<param-name>contextInitializerClasses</param-name>
		<param-value>{ string-join ((
			'txt2.servlet.Txt2WebContextInitializer'
		), ' ') }</param-value>
	</context-param>

	<!-- =============================== listeners -->

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<listener>
		<listener-class>org.springframework.web.context.request.RequestContextListener</listener-class>
	</listener>

	<!-- =============================== filters -->

	<filter>
		<filter-name>responseFilter</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>

	<!-- =============================== filter mappings -->

	<filter-mapping>
		<filter-name>responseFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- =============================== servlets -->

	<servlet>
		<display-name>g8wave</display-name>
		<servlet-name>G8waveApiServlet</servlet-name>
		<servlet-class>txt2.g8wave.webapi.G8waveApiServlet</servlet-class>
	</servlet>

	<servlet>
		<display-name>default</display-name>
		<servlet-name>DefaultServlet</servlet-name>
		<servlet-class>txt2.servlet.PathHandlerServlet</servlet-class>
		<init-param>
			<param-name>pathHandler</param-name>
			<param-value>rootPathHandler</param-value>
		</init-param>
		<init-param>
			<param-name>exceptionHandler</param-name>
			<param-value>exceptionHandler</param-value>
		</init-param>
	</servlet>

	<!-- =============================== servlet mappings -->

	<servlet-mapping>
		<servlet-name>DefaultServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

	<servlet-mapping>
		<servlet-name>G8waveApiServlet</servlet-name>
		<url-pattern>/g8wave/*</url-pattern>
	</servlet-mapping>

</web-app>
