package test;

public class SumOfMultiples {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("usage : SumOfMultiples 5 7");
			return;
		}

		int a = 0, b = 0;
		try {
			a = Integer.parseInt(args[0]);
			b = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("숫자만 입력 가능 합니다.");
		}

		// 1000 보다 작은 배수의 수
		int multipleCountOfa = 1000 / a;
		int multipleCountOfb = 1000 / b;
		// 두 수의 최소 공배수의 배수의 수
		int lcm = a * b / gcd(a, b);
		int multipleCountOflcm = 1000 / lcm;

		// N번째 까지 임의의 수 a 의 배수의 합 : N*(N+1)/2*a
		// a의 배수의 합 + b의 배수의 합 - (a 와 b 의 공배수의 합)
		int mulipleSum = (multipleCountOfa * (multipleCountOfa + 1) / 2 * a) + (multipleCountOfb * (multipleCountOfb + 1) / 2 * b) - (multipleCountOflcm * (multipleCountOflcm + 1) / 2 * lcm);
		System.out.println(a + " 와 " + b + " 의 배수의 합 : " + mulipleSum);
	}

	// 최대 공약수
	private static int gcd(int a, int b) {
		if (b == 0) {
			return a;
		}
		return gcd(b, a % b);
	}
}
