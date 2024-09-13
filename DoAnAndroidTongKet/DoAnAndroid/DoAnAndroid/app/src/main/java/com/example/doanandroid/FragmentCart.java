package com.example.doanandroid;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.doanandroid.POJO.CuaHang;
import com.example.doanandroid.POJO.DonHangInnerJoin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCart#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCart extends Fragment {

    ListView lvDonHAngInnerJoin;
    ArrayList<DonHangInnerJoin> dsdhinnerjoin = new ArrayList<>();
    CustomAdapterDonHangFC customAdapterDonHangFC;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentCart() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentCart.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCart newInstance(String param1, String param2) {
        FragmentCart fragment = new FragmentCart();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_cart, container, false);
        lvDonHAngInnerJoin = view.findViewById(R.id.lvDonHangFC);
        // Lấy dữ liệu từ SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int maKhachHang = sharedPreferences.getInt("maKhachHang", 0);
        String url = getResources().getString(R.string.url)+"DuLieuDonHangVuaTao.php?MaKhachHang="+maKhachHang;
        getAllDataDonHangInnerJoin(url);
        //Xử lí sự kiện khi click item listView
        lvDonHAngInnerJoin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy sản phẩm được chọn từ danh sách hiển thị
                customAdapterDonHangFC = (CustomAdapterDonHangFC) lvDonHAngInnerJoin.getAdapter();
                dsdhinnerjoin = customAdapterDonHangFC.getListCuaHang();
                DonHangInnerJoin selectedCuaHang = dsdhinnerjoin.get(position);
                // Tạo Intent để mở activity mới
                Intent intent = new Intent(getContext(), HoaDonActivity.class);

                // Truyền dữ liệu của sản phẩm sang activity mới
                intent.putExtra("MA_CUA_HANG", selectedCuaHang.getMaCuaHang());
                // Bắt đầu activity mới
                startActivity(intent);
            }
        });
        //Xử lí sự kiện khi longClick listView
        lvDonHAngInnerJoin.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy mục được chọn từ danh sách hiển thị
                DonHangInnerJoin selectedItem = dsdhinnerjoin.get(position);

                // Hiển thị hộp thoại xác nhận xóa
                new AlertDialog.Builder(getContext()).setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa đơn của cửa hàng này ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Xác nhận xóa: Tìm mục tương ứng trong danh sách chính thức và xóa nó
                                if (selectedItem != null) {
                                    for (DonHangInnerJoin dh : dsdhinnerjoin) {
                                        if (dh.equals(selectedItem)) {
                                            int maDonHangXoa = dh.getMaDonHang();
                                            String urlXoaChiTietDonHang = getResources().getString(R.string.url)+"thuchientruyvan.php?query="+"DELETE FROM `chitietdonhang` WHERE MaDonHang = '"+maDonHangXoa+"'";
                                            khoiChay(urlXoaChiTietDonHang);
                                            String urlXoaDonHang = getResources().getString(R.string.url)+"thuchientruyvan.php?query="+"DELETE FROM `DonHang` WHERE MaDonHang = '"+maDonHangXoa+"'";
                                            khoiChay(urlXoaDonHang);
                                            dsdhinnerjoin.remove(dh);
                                            break;
                                        }
                                    }
                                    // Cập nhật Adapter của ListView
                                    customAdapterDonHangFC.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                // Trả về true để chỉ ra rằng sự kiện long click đã được xử lý
                return true;
            }
        });
        return view;
    }
    public void khoiChay(String url) {
        new MyUrlTask().execute(url);
    }
    public void getAllDataDonHangInnerJoin(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            parseJsonDataDHIJ(response);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(stringRequest);

    }

    public void parseJsonDataDHIJ(String response) throws JSONException {

        JSONArray cuaHangArray = new JSONArray(response);
        for (int i = 0; i < cuaHangArray.length(); i++) {
            JSONObject cuaHang = cuaHangArray.getJSONObject(i);
            DonHangInnerJoin a = new DonHangInnerJoin();
            a.setMaDonHang(Integer.parseInt(cuaHang.getString("MaDonHang")));
            a.setMaKhachHang(Integer.parseInt(cuaHang.getString("MaKhachHang")));
            a.setMaCuaHang(Integer.parseInt(cuaHang.getString("MaCuaHang")));
            a.setMaMon(Integer.parseInt(cuaHang.getString("MaMon")));
            a.setSoLuong(Integer.parseInt(cuaHang.getString("SoLuong")));
            a.setThanhTien(Integer.parseInt(cuaHang.getString("ThanhTien")));
            a.setTrongGioHang(Integer.parseInt(cuaHang.getString("TrongGioHang")));
            a.setTenMon(cuaHang.getString("TenMon"));
            a.setGiaMon(Integer.parseInt(cuaHang.getString("GiaMon")));
            a.setGiaBan(Integer.parseInt(cuaHang.getString("GiaBanRa")));
            a.setTenMon(cuaHang.getString("AnhMon"));
            a.setTenCuaHang(cuaHang.getString("TenCuaHang"));
            a.setHinhAnhDaiDien(cuaHang.getString("HinhAnhDaiDien"));
            a.setKhoangCachDiaLy(Double.parseDouble(cuaHang.getString("KhoangCachDiaLy")));
            a.setDangHoatDong(Integer.parseInt(cuaHang.getString("DangHoatDong")));
            boolean maCuaHangDaTonTai = false;

            // Duyệt qua danh sách đơn hàng để kiểm tra
            for (DonHangInnerJoin donHang : dsdhinnerjoin) {
                if (donHang.getMaCuaHang() == a.getMaCuaHang()) {
                    // Nếu mã cửa hàng đã tồn tại trong danh sách, đặt biến kiểm tra là true
                    maCuaHangDaTonTai = true;
                    break;
                }
            }

            // Nếu mã cửa hàng không tồn tại trong danh sách, thêm mới
            if (!maCuaHangDaTonTai && a.getTrongGioHang() == 1) {
                // Thêm mới đơn hàng vào danh sách
                dsdhinnerjoin.add(a);
            }
        }
        customAdapterDonHangFC = new CustomAdapterDonHangFC(getContext(), R.layout.item_don_hang_trong_gio, dsdhinnerjoin);
        lvDonHAngInnerJoin.setAdapter(customAdapterDonHangFC);
    }
}