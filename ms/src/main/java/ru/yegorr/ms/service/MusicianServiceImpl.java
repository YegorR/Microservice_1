package ru.yegorr.ms.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import ru.yegorr.ms.dto.response.*;
import ru.yegorr.ms.entity.*;
import ru.yegorr.ms.exception.*;
import ru.yegorr.ms.repository.*;

import java.util.*;
import java.util.stream.*;

@Service
public class MusicianServiceImpl implements MusicianService {

  private final MusicianRepository musicianRepository;

  @Autowired
  public MusicianServiceImpl(MusicianRepository musicianRepository) {
    this.musicianRepository = musicianRepository;
  }

  @Transactional
  @Override
  public MusicianDto createMusician(String name, String description) {
    MusicianEntity musician = new MusicianEntity();
    musician.setName(name);
    musician.setDescription(description);
    musician = musicianRepository.save(musician);

    return translateEntityToDto(musician);
  }

  @Transactional
  @Override
  public MusicianDto changeMusician(Long id, String name, String description)
          throws ClientException {
    MusicianEntity musician = musicianRepository.findById(id).
            orElseThrow(() -> new ClientException(HttpStatus.NOT_FOUND,
                    "The musician is not exists"));

    musician.setName(name);
    musician.setDescription(description);
    musician.setMusicianId(id);

    musician = musicianRepository.save(musician);
    return translateEntityToDto(musician);
  }

  @Transactional
  @Override
  public void deleteMusician(Long id) throws ClientException {
    if (!musicianRepository.existsById(id)) {
      throw new ClientException(HttpStatus.NOT_FOUND, "The musician is not exists");
    }
    musicianRepository.deleteById(id);
  }

  @Transactional
  @Override
  public MusicianDto getMusician(Long id) throws ClientException {
    Optional<MusicianEntity> musician = musicianRepository.findById(id);
    if (!musician.isPresent()) {
      throw new ClientException(HttpStatus.NOT_FOUND, "The musician is not exists");
    }
    return translateEntityToDto(musician.get());
  }


  private MusicianDto translateEntityToDto(MusicianEntity entity) {
    MusicianDto musicianDto = new MusicianDto();
    musicianDto.setId(entity.getMusicianId());
    musicianDto.setDescription(entity.getDescription());
    musicianDto.setName(entity.getName());

    List<AlbumEntity> albumsAndSingles = entity.getAlbums();

    if (albumsAndSingles == null) {
      return musicianDto;
    }

    List<AlbumDescriptionDto> albums = new ArrayList<>();
    List<AlbumDescriptionDto> singles = new ArrayList<>();

    for (AlbumEntity release : albumsAndSingles) {
      AlbumDescriptionDto releaseDto = new AlbumDescriptionDto();
      releaseDto.setId(release.getAlbumId());
      releaseDto.setName(release.getName());
      releaseDto.setReleaseDate(release.getReleaseDate());
      releaseDto.setSingle(release.getSingle());

      if (release.getSingle()) {
        singles.add(releaseDto);
      } else {
        albums.add(releaseDto);
      }
    }

    musicianDto.setSingles(singles);
    musicianDto.setAlbums(albums);

    return musicianDto;
  }

  @Override
  @Transactional
  public List<BriefMusicianDto> searchMusicians(String query) {
    return musicianRepository.findAllByNameContainingIgnoreCase(query).stream().
            map((entity) -> {
              BriefMusicianDto dto = new BriefMusicianDto();
              dto.setName(entity.getName());
              dto.setId(entity.getMusicianId());
              return dto;
            }).collect(Collectors.toList());
  }
}
