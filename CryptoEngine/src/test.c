#include <tc.h>
#include "mathutils.h"
#include <gmp.h>
#include <check.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "test.h"


int main()
{
	// Generate keys
	size_t key_size = 512;
	int threshold = 4;
	int nodes = 7;
	
	key_metainfo_t * info;
	key_share_t ** shares = tc_generate_keys(&info, key_size, threshold, nodes, NULL);
	
	// Serialize the keys
	int l = tc_key_meta_info_l(info);
	//ck_assert_int_eq(l, nodes);
	char * serialized_shares[l];
	char * serialized_info;
	
	for(int i = 0; i < l; i++)
	{
		serialized_shares[i] = tc_serialize_key_share(shares[i]);
	}
	
	serialized_info = tc_serialize_key_metainfo(info);
	
	// Clear original shares
	tc_clear_key_shares(shares, info);

	

	

	//printf("%s \n", shares);
	return 0;
}

