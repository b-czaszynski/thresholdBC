#include "test.h"


void main()
{
	// Generate keys
	size_t key_size = 512;
	int threshold = 4;
	int nodes = 7;
	
	key_metainfo_t * info;
	key_share_t ** shares = tc_generate_keys(&info, key_size, threshold, nodes, NULL);
	
	// Serialize the keys
	int l = tc_key_meta_info_l(info);
	char * serialized_shares[l];
	char * serialized_info;
	
	printf("%s\n", "Serialized key shares:");
	for(int i = 0; i < l; i++)
	{
		serialized_shares[i] = tc_serialize_key_share(shares[i]);
		printf("%d - %s\n", i, serialized_shares[i]);
	}
	
	serialized_info = tc_serialize_key_metainfo(info);
	printf("%s\n", "Serialized key info:");
	printf("%s\n", serialize_public_key(info->public_key));
	// Clear original shares
	tc_clear_key_shares(shares, info);

	

}



bytes_t * processMessage(char * message, key_metainfo_t * info)
{
	bytes_t * doc = tc_init_bytes( message, strlen(message));
    bytes_t * doc_pkcs1 = tc_prepare_document(doc, TC_SHA256, info);

    return doc_pkcs1;
}


void printMessage(bytes_t * msg)
{
	char * b64 = tc_bytes_b64(msg);
    printf("Document: %s\n", b64);
    free(b64);
}





