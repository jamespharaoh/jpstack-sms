package wbs.utils.etc;

public
interface SafeCloseable
	extends AutoCloseable {

	@Override
	void close ();

}
