SELECT router_type_insert (
	'route',
	'static',
	'Always send to the same route');

SELECT router_type_insert (
	'simple_router',
	'default',
	'Send to the configured route');

SELECT priv_type_insert (
	'simple_router',
	'manage',
	'Full control',
	'Full control of this simple router',
	true);
