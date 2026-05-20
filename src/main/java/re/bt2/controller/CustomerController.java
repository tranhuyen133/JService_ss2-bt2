package re.bt2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
//HTTP POST thường được dùng để tạo mới tài nguyên. Mỗi lần gửi POST, server có thể tạo ra một dữ liệu mới
// nên POST không có tính idempotent. Điều này có nghĩa là nếu gửi cùng một request nhiều lần thì có thể sinh ra
// nhiều bản ghi khác nhau.
//Ngược lại, HTTP PUT được dùng để cập nhật một tài nguyên đã tồn tại. PUT có tính idempotent, nghĩa là gửi nhiều lần
// cùng một dữ liệu thì kết quả cuối cùng vẫn không thay đổi. PUT chỉ cập nhật dữ liệu hiện có chứ không nên tạo thêm
// bản ghi mới.
//Trong đoạn mã trên, phương thức createOrUpdateCustomer() sử dụng POST cho cả tạo mới và cập nhật. Khi client gửi dữ liệu
// cập nhật nhưng ID không tồn tại, chương trình lại thực hiện:

//customers.add(customer);

//điều này làm phát sinh thêm khách hàng mới thay vì báo lỗi. Vì POST không đảm bảo idempotent nên khi người dùng bấm
// cập nhật nhiều lần hoặc gửi request lặp lại có thể tạo ra dữ liệu trùng lặp và không nhất quán.
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private List<Customer> customers = new ArrayList<>();
    private AtomicLong nextId = new AtomicLong(1);

    static class Customer {
        private Long id;
        private String name;
        private String email;

        public Customer() {
        }

        public Customer(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // API tạo mới khách hàng
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {

        customer.setId(nextId.getAndIncrement());

        customers.add(customer);

        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    // API cập nhật khách hàng
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer) {

        Optional<Customer> existingCustomer = customers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        if (existingCustomer.isPresent()) {

            Customer c = existingCustomer.get();

            c.setName(customer.getName());
            c.setEmail(customer.getEmail());

            return new ResponseEntity<>(c, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {

        return customers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
