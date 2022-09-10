package me.ingannatore.dddkata.repo

import me.ingannatore.dddkata.entity.BacklogItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BacklogItemRepository : JpaRepository<BacklogItem, Long>
