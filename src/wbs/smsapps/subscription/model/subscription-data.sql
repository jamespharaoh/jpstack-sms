---------------------------------------- INSERT object_type

SELECT object_type_insert (
	'subscription',
	'subscription',
	'slice',
	1);

SELECT object_type_insert (
	'subscription_affiliate',
	'subscription affiliate',
	'subscription',
	1);

SELECT object_type_insert (
	'subscription_send',
	'subscription send',
	'subscription',
	3);

SELECT object_type_insert (
	'subscription_send_part',
	'subscription send part',
	'subscription_send',
	2);

SELECT object_type_insert (
	'subscription_send_number',
	'subscription send number',
	'subscription_send',
	3);

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'subscription',
	'subscribe',
	'Subscribe');

SELECT command_type_insert (
	'subscription',
	'unsubscribe',
	'Unsubscribe');

SELECT command_type_insert (
	'subscription_affiliate',
	'subscribe',
	'Subscribe');

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'subscription',
	'default',
	'Default');

---------------------------------------- INSERT messageset_type

SELECT message_set_type_insert (
	'subscription',
	'subscribe_success',
	'Subscribe successful');

SELECT message_set_type_insert (
	'subscription',
	'subscribe_already',
	'Subscription failed because already subscribed');

SELECT message_set_type_insert (
	'subscription',
	'unsubscribe_success',
	'Unsubscribe successful');

SELECT message_set_type_insert (
	'subscription',
	'unsubscribe_already',
	'Unsubscription failed because already unsubscribed');

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'subscription',
	'manage',
	'Full control',
	'Full control of this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'admin',
	'Admin tasks',
	'Perform administrative tasks on this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'subscription_send',
	'Send to subscription',
	'Send messages to users who are subscribed to this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'stats',
	'View message stats',
	'View message stats for this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'messages',
	'View message history',
	'View message histroy for this subscription',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'admin',
	'Admin tasks',
	'Perform administrative tasks on this subscription affiliage',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'stats',
	'View message stats',
	'View message stats for this subscription affiliate',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'messages',
	'View message history',
	'View message history for this subscription affiliate',
	true);

SELECT priv_type_insert (
	'slice',
	'subscription_create',
	'Create subscription',
	'Create subscriptions in this slice',
	true);

SELECT priv_type_insert (
	'slice',
	'subscription_list',
	'List subscriptions',
	'List all subscriptions in this slice',
	true);

---------------------------------------- INSERT batch_type

SELECT batch_type_insert (
	'subscription',
	'send',
	'Subscription send',
	'subscription_send');

--------------------------------------- INSERT template_type

SELECT template_type_insert (
	'subscription_send',
	'default',
	'Messages to send');

--------------------------------------- INSERT delivery_type

SELECT delivery_type_insert (
	'subscription',
	'Subscription');

---------------------------------------- INSERT affiliate_type

SELECT affiliate_type_insert (
	'subscription_affiliate',
	'default',
	'Default');