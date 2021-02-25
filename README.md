# MyMusic
Các thành phần chính của phần mềm Nghe nhạc
ActivityMusic extends AppCompatActivity
+ Là Activity chính của phần mềm.
+ Quản lý các chức năng liên quan tới vòng đời Activity.
+ Quản lý việc load các Fragment cần hiển thị.

AllSongsFragment extends Fragment
+ Hiển thị một danh sách bài hát được chỉ định như mô tả ở bài tập 1.

MediaPlaybackFragment extends Fragment
+ Hiển thị nội dung đang chơi 1 bài hát như mô tả ở bài tập 1.

Service nghe nhạc là MediaPlaybackService extends Service.
+ Quản lý danh sách bài hát đang chơi.
+ Quản lý logic liên quan tới chơi 1 bài, tự động chơi bài tiếp theo khi hết bài, pause / play, next, previous, repeat, shuffle.
+ Quản lý object MediaPlayer để chơi nhạc.
+ Quản lý thông báo điều khiển nhạc.

FavoriteSongsProvider extends ContentProvider
+ Quản lý các logic liên quan tới CSDL Sqlite, lưu các bài hát được người dùng chọn là “yêu thích”.

