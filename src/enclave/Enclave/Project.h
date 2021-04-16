#include <stdarg.h>
#include <stdio.h>      /* vsnprintf */
#include <stdint.h>

#include "Enclave.h"
#include "Enclave_t.h"  /* print_string */
#include "sgx_trts.h"
#include "Crypto.h"
#include "util.h"
#include "NewInternalTypes.h"
#include "EncryptedDAG.h"

#ifndef PROJECT_H
#define PROJECT_H

void project(int op_code,
             Verify *verify_set,
             uint8_t *input_rows, uint32_t input_rows_length,
             uint32_t num_rows,
             uint8_t *output_rows, uint32_t output_rows_length,
             uint32_t *actual_output_rows_length);

void project_single_row(int op_code, NewRecord *in, NewRecord *out);

#endif // PROJECT_H
