package shn.shopify.logic;

import java.util.Collection;
import java.util.List;

import wbs.framework.database.Transaction;

import shn.product.model.ShnProductRec;
import shn.shopify.model.ShnShopifyConnectionRec;

public
interface ShnShopifyLogic {

	String productBodyHtml (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			ShnProductRec product);

	<Local, RemoteRequest, RemoteResponse>
	List <String> compareAttributes (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			Local local,
			RemoteResponse remote);

	<Local, RemoteRequest, RemoteResponse>
	List <String> compareCollection (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			List <Local> local,
			List <RemoteResponse> remote,
			String singularName,
			String pluralName);

	<Local, RemoteRequest, RemoteResponse>
	RemoteRequest createRequest (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			Local local,
			Class <RemoteRequest> remoteRequestClass);

	<Local, RemoteRequest, RemoteResponse>
	List <RemoteRequest> createRequestCollection (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			Collection <Local> local,
			Class <RemoteRequest> remoteRequestClass);

}
