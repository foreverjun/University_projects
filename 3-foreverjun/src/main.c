#include <stdio.h>
#include <math.h>

int main()
{
    const double eps = 0.000001;
    float f_x1, f_x2, f_deviation = 0.0001f, f_range = 1;
    double d_x1, d_x2, d_deviation = 0.0001, d_range = 1;
    int i = 0;
    //when performing multiple calculations with float
    printf("Float with all calculations.\n");
    while (f_range > eps && i < 25) {
        f_x2 = (2.0001f + f_deviation - 2.f) / (1.0001f - 1.f);
        f_x1 = 2.f - f_x2;
        f_range = sqrtf((f_x1 - 1.f) * (f_x1 - 1.f) + (f_x2 - 1.f) * (f_x2 - 1.f));
        i++;
        printf("Experiment number %d : x1 = %.16f   x2 = %.16f   deviation = %.16f   range = %.16f\n", i, f_x1, f_x2,f_deviation, f_range);
        f_deviation = f_deviation / 2.f;
    }
    i = 0;
    //when performing multiple calculations with double
    printf("Double with all calculations.\n");
    while (d_range > eps){
        d_x2 = (2.0001 + d_deviation - 2)/(1.0001 - 1);
        d_x1 = 2 - d_x2;
        d_range = sqrt((d_x1 - 1)*(d_x1 - 1) + (d_x2 - 1)*(d_x2 - 1));
        i++;
        printf("Experiment number %d : x1 = %.16lf   x2 = %.16lf   deviation = %.16lf   range = %.16lf\n", i, d_x1, d_x2, d_deviation, d_range);
        d_deviation = d_deviation/2;
    }
    i = 0;
    f_deviation = 0.0001f;
    d_deviation = 0.0001;
    f_range = 1;
    d_range = 1;
    //when performing two calculations with float
    printf("Float with two calculations.\n");
    while (f_range > eps) {
        f_x2 = (0.0001f  + f_deviation)/0.0001f;
        f_x1 = 2.f - f_x2;
        f_range = sqrtf((f_x1 - 1.f) * (f_x1 - 1.f) + (f_x2 - 1.f) * (f_x2 - 1.f));
        i++;
        printf("Experiment number %d : x1 = %.16f   x2 = %.16f   deviation = %.16f   range = %.16f\n", i, f_x1, f_x2, f_deviation, f_range);
        f_deviation = f_deviation / 2.f;
    }
    i = 0;
    //when performing two calculations with double
    printf("Double with two calculations.\n");
    while (d_range > eps){
        d_x2 = (0.0001  + d_deviation)/0.0001;
        d_x1 = 2 - d_x2;
        d_range = sqrt((d_x1 - 1)*(d_x1 - 1) + (d_x2 - 1)*(d_x2 - 1));
        i++;
        printf("Experiment number %d : x1 = %.16lf   x2 = %.16lf   deviation = %.16lf   range = %.16lf\n", i, d_x1, d_x2, d_deviation, d_range);
        d_deviation = d_deviation/2;
    }
}
