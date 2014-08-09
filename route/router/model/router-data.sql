
SELECT object_type_insert (
	'router_type',
	'router type',
	'object_type',
	2);

SELECT object_type_insert (
	'router',
	'router',
	NULL,
	2);

SELECT object_type_insert (
	'simple_router',
	'simple router',
	'slice',
	1);

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
