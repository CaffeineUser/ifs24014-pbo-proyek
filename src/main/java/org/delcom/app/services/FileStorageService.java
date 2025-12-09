package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    // Lokasi folder upload, default ke ./uploads
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    // Daftar ekstensi yang diperbolehkan (Keamanan)
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    /**
     * Menyimpan gambar untuk Menu Item (menggunakan Long ID)
     */
    public String storeFile(MultipartFile file, Long entityId, String typePrefix) {
        return storeFileInternal(file, String.valueOf(entityId), typePrefix);
    }

    /**
     * Menyimpan gambar untuk User/Profile (menggunakan UUID)
     */
    public String storeFile(MultipartFile file, UUID entityId, String typePrefix) {
        return storeFileInternal(file, entityId.toString(), typePrefix);
    }

    /**
     * Internal logic untuk menyimpan file
     */
    private String storeFileInternal(MultipartFile file, String identifier, String typePrefix) {
        try {
            // 1. Validasi File Kosong
            if (file.isEmpty()) {
                throw new RuntimeException("Gagal menyimpan file kosong.");
            }

            // 2. Bersihkan nama file & Ambil Ekstensi
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);

            // 3. Validasi Ekstensi (Hanya Gambar)
            if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                throw new RuntimeException("Format file tidak didukung. Harap upload: " + ALLOWED_EXTENSIONS);
            }

            // 4. Pastikan folder ada
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 5. Generate Nama Baru: menu_123.jpg atau avatar_uuid.png
            // Menggunakan System.currentTimeMillis() agar browser tidak cache gambar lama jika diupdate
            String newFilename = typePrefix + "_" + identifier + "_" + System.currentTimeMillis() + "." + fileExtension;

            // 6. Simpan File
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Gagal menyimpan file " + typePrefix, ex);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) return;
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error tapi jangan throw exception agar tidak mengganggu proses utama
            System.err.println("Warning: Gagal menghapus file lama " + filename);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File tidak ditemukan: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File tidak ditemukan: " + filename, ex);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}