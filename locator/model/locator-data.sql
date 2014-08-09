
SELECT object_type_insert (
	'biaxial_ellipsoid',
	'biaxial ellipsoid',
	'root',
	1);

SELECT object_type_insert (
	'mercator_projection',
	'mercator projection',
	'root',
	1);

--SELECT biaxial_ellipsoid_insert (
--	'airy_1830',
--	'Airy 1830',
--	6377563.396,
--	6356256.910);

--SELECT biaxial_ellipsoid_insert (
--	'airy_1830_modified',
--	'Airy 1830 modified',
--	6377340.189,
--	6356034.447);

--SELECT biaxial_ellipsoid_insert (
--	'int_1924',
--	'International 1924 aka Hayford 1909',
--	6378388.000,
--	6356911.946);

--SELECT biaxial_ellipsoid_insert (
--	'grs80',
--	'GRS80 aka WGS83 ellipsoid',
--	6378137.000,
--	6356752.3141);

--SELECT mercator_projection_insert (
--	'uk_national_grid',
--	'UK National Grid (OSGB)',
--	'airy_1830',
--	0.9996012717,
--	radians (49),
--	radians (-2),
--	-100000,
--	400000);

--SELECT mercator_projection_insert (
--	'irish_national_grid',
--	'Irish National Grid',
--	'airy_1830_modified',
--	1.000035,
--	radians (-53 - 30/60),
--	radians (8),
--	250000,
--	200000);

--SELECT mercator_projection_insert (
--	'utm_29',
--	'UTM zone 29',
--	'int_1924',
--	0.9996,
--	radians (0),
--	radians (-9),
--	0,
--	500000);

--SELECT mercator_projection_insert (
--	'utm_30',
--	'UTM zone 30',
--	'int_1924',
--	0.9996,
--	radians (0),
--	radians (-3),
--	0,
--	500000);

--SELECT mercator_projection_insert (
--	'utm_31',
--	'UTM zone 31',
--	'int_1924',
--	0.9996,
--	radians (0),
--	radians (3),
--	0,
--	500000);

SELECT object_type_insert (
	'locator_type',
	'locator type',
	'object_type',
	2);

SELECT object_type_insert (
	'locator',
	'locator',
	NULL,
	2);

SELECT object_type_insert (
	'locator_log',
	'locator log',
	'root',
	3);
