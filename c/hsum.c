#include <immintrin.h> // x86
#include <stdio.h>

int main(int argc, char **argv) {
  if (argc < 9) {
    printf("Usage: hsum <8 floats>");
    return 1;
  }
  __m128 vector1 = _mm_set_ps(strtof(argv[1], NULL), strtof(argv[2], NULL),
                              strtof(argv[3], NULL), strtof(argv[4], NULL));
  __m128 vector2 = _mm_set_ps(strtof(argv[5], NULL), strtof(argv[6], NULL),
                              strtof(argv[7], NULL), strtof(argv[8], NULL));
  __mmask16 k;

  __m128 sum = _mm_hadd_ps(vector1, vector2);
  sum = _mm_hadd_ps(sum, sum);
  sum = _mm_hadd_ps(sum, sum);
  // extract float in a horrible way
  float hsum = _mm_cvtss_f32(_mm_shuffle_ps(sum, sum, _MM_SHUFFLE(0, 0, 0, 2)));
  printf("%f\n", hsum);

  return 0;
}
